//
// Created by alon on 15.01.17.
//
#include <stdio.h>
#include <curl/curl.h>
#include <thread>
#include <chrono>
#include <sstream>
#include "Helpers/HelpFunctions.h"
#include "MainWindow.h"

enum MetricColumns
{
    COL_ID = 0,
    COL_FOCUSED_WINDOW,
    COL_CLIENT_WINDOW,
    COL_WINDOW_TITLE,
    COL_RESOURCE_WINDOW,
    COL_RESOURCE_CLASS,
    COL_PATH,
    COL_URL,
    COL_PID,
    COL_CONNECT_TIME,
    COL_DISCONNECT_TIME,
    COL_SENT,
    COL_FILTERED,
    COL_TIME,
    NUM_COLS
};

sqlite3 *dbSendDemon;
extern Config config;

void put_value(std::stringstream &sstream, const char *name, const char *type, const char *value){
    sstream << "{ ";
    sstream << "\"name\":\"" << name <<"\", ";
    sstream << "\"type\":\"" << type <<"\", ";
    if(strlen(value)==0)
        sstream << "\"value\":\"-\"";
    else{
        std::string str = Helper::escape_json(value);
        sstream << "\"value\":\"" << str <<"\"";
    }

    sstream << " }";
}

template <typename T>
void put_value(std::stringstream &sstream, const char *name, const char *type, const T value){
    sstream << "{ ";
    sstream << "\"name\":\"" << name <<"\", ";
    sstream << "\"type\":\"" << type <<"\", ";
    sstream << "\"value\":\"" << value <<"\"";
    sstream << " }";
}

static std::vector<string> filterString = std::vector<string>();

void prepare_filter_string(int rc){
    sqlite3_stmt *stmt;
    rc = sqlite3_prepare_v2(dbSendDemon, DbQueries::SelectTitleFilter().c_str(), -1, &stmt, 0);
    while (sqlite3_step(stmt) == SQLITE_ROW){
        filterString.push_back(reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0)));
    }
}

std::vector<string> get_filters(){
    std::vector<string> filters = std::vector<string>();

    sqlite3_stmt *stmt;
    int rc = sqlite3_prepare_v2(dbSendDemon, DbQueries::SelectOnlyTitleFilter().c_str(), -1, &stmt, 0);

    while (sqlite3_step(stmt) == SQLITE_ROW){
        filters.push_back(string(reinterpret_cast<const char*>(sqlite3_column_text(stmt, 0))));
    }

    return filters;
}

string getFilterString(){
    static std::vector<string> filters = get_filters();

    if(filters.size()<1)
        return "";
    std::stringstream sstreamUpdate;
    sstreamUpdate << DbQueries::FilterMetrics().c_str();
    std::vector<string>::iterator it = filters.begin();
    sstreamUpdate << "WindowTitle LIKE '%" << *it << "%' OR ResourceName LIKE '%" << *it << "%' OR ResourceClass LIKE '%" << *it;
    for (it++; it != filters.end(); ++it){
        sstreamUpdate << "%' OR ";
        sstreamUpdate << "WindowTitle LIKE '%" << *it << "%' OR ResourceName LIKE '%" << *it << "%' OR ResourceClass LIKE '%" << *it;
    }
    sstreamUpdate << "%';";
    return sstreamUpdate.str();
}

void get_time_filters(std::vector<int> &filters, std::vector<int> &filters2){

    sqlite3_stmt *stmt;
    int rc = sqlite3_prepare_v2(dbSendDemon, DbQueries::SelectOnlyTimeFilter().c_str(), -1, &stmt, 0);

    while (sqlite3_step(stmt) == SQLITE_ROW){
        filters.push_back(sqlite3_column_int(stmt, 0));
        filters2.push_back(sqlite3_column_int(stmt, 1));
    }
}

string getTimeFilterString(){
    static bool gotFilters= false;
    static std::vector<int> filter1 =  std::vector<int>();
    static std::vector<int> filter2 =  std::vector<int>();
    if(!gotFilters){
        get_time_filters(filter1, filter2);
        gotFilters = true;
    }


    if(filter1.size()<1)
        return "";
    std::stringstream sstreamUpdate;
    sstreamUpdate << DbQueries::FilterMetrics().c_str();
    std::vector<int>::iterator it = filter1.begin();
    std::vector<int>::iterator it2 = filter2.begin();
    sstreamUpdate << "(Time >= " << *it << " AND Time <= " << *it2 << ")";
    for (it++; it != filter1.end(); ++it, ++it2){
        sstreamUpdate << " OR ";
        sstreamUpdate << "(Time >= " << *it << " AND Time <= " << *it2 << ")";;
    }
    sstreamUpdate << ";";
    return sstreamUpdate.str();
}

void filter_data(){
    static string data = getFilterString();

    if(data == "")
        return;

    sqlite3_stmt *stmt;
    int rc = sqlite3_prepare_v2(dbSendDemon, data.c_str(), -1, &stmt, 0);
    rc = sqlite3_step(stmt);

    sqlite3_finalize(stmt);

    if(rc == SQLITE_DONE){
//        printf("%s\n", data.c_str());
        printf("Title Filtered\n");
    }
}

void filter_time_data(){
    static string data = getTimeFilterString();

    if(data == "")
        return;

    sqlite3_stmt *stmt;
    int rc = sqlite3_prepare_v2(dbSendDemon, data.c_str(), -1, &stmt, 0);
    rc = sqlite3_step(stmt);

    sqlite3_finalize(stmt);

    if(rc == SQLITE_DONE){
//        printf("%s\n", data.c_str());
        printf("Time Filtered\n");
    }
}

struct curl_slist *global_cookies = NULL;

bool GetLastCookies(){
    return global_cookies != NULL;
}

void getCookies(const char *user_name, const char *pass, int &code){
    CURL *curl;
    CURLcode res;
    /* get a curl handle */
    curl = curl_easy_init();
    if(curl) {
        struct curl_slist *headers = NULL;
        std::string readBuffer;
        struct curl_httppost *formpost=NULL;
        struct curl_httppost *lastptr=NULL;

        curl_formadd(&formpost,
                     &lastptr,
                     CURLFORM_COPYNAME, "email",
                     CURLFORM_COPYCONTENTS, user_name,
                     CURLFORM_END);
        curl_formadd(&formpost,
                    &lastptr,
                    CURLFORM_COPYNAME, "password",
                    CURLFORM_COPYCONTENTS, pass,
                    CURLFORM_END);


        headers = curl_slist_append(headers, "Content-Type: multipart/form-data");
        headers = curl_slist_append(headers, "charsets: utf-8");
        std::string url = config.server_url + "login";
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());

        curl_easy_setopt(curl, CURLOPT_HTTPHEADER, headers);

        /* Now specify the POST data */
        curl_easy_setopt(curl, CURLOPT_HTTPPOST, formpost);

        curl_easy_setopt(curl, CURLOPT_WRITEFUNCTION, WriteCallback);
        curl_easy_setopt(curl, CURLOPT_WRITEDATA, &readBuffer);
        curl_easy_setopt(curl, CURLOPT_COOKIEFILE, "");

        /* Perform the request, res will get the return code */
        res = curl_easy_perform(curl);
        /* Check for errors */
        if(res != CURLE_OK){
            fprintf(stderr, "curl_easy_perform() failed: %s\n",
                    curl_easy_strerror(res));
        }

        int http_code = 0;
        curl_easy_getinfo (curl, CURLINFO_RESPONSE_CODE, &http_code);
        printf("\nCODE: %d\n", http_code);

        code = http_code;

        if(http_code==200){
            curl_easy_getinfo(curl, CURLINFO_COOKIELIST, &global_cookies);
        }
//        std::cout << http_code << " " << readBuffer << std::endl;
        /* always cleanup */
        curl_easy_cleanup(curl);

        return;
//        curl_global_cleanup();
    }
}

bool send_static_data(){
//    static int rc = sqlite3_open(SQLITE_PATH, &dbSendDemon);
    int rc;
    sqlite3_stmt *stmt;

    CURL *curl;
    CURLcode res;

    rc = sqlite3_prepare_v2(dbSendDemon, DbQueries::GetLastStaticMetric().c_str(), -1, &stmt, 0);

    int count = 0;

    std::stringstream sstream;
    sstream << "{ \"activity\": { \"activities\":[ ";

    while (sqlite3_step(stmt) == SQLITE_ROW){
        if(count > 0)
            sstream << ", ";
        sstream << "{ \"name\":\"LinuxTool-SystemInfo\",\"measurements\":[";

        put_value(sstream, "Os User Name", "string", reinterpret_cast<const char*>(sqlite3_column_text(stmt, 1)));
        sstream << ", ";
        put_value(sstream, "Os Host Name", "string", reinterpret_cast<const char*>(sqlite3_column_text(stmt, 2)));
        sstream << ", ";
        put_value(sstream, "Network names", "string", reinterpret_cast<const char*>(sqlite3_column_text(stmt, 3)));
        sstream << ", ";
        put_value(sstream, "Network Ips", "string", reinterpret_cast<const char*>(sqlite3_column_text(stmt, 4)));
        sstream << ", ";
        put_value(sstream, "Network MACs", "string", reinterpret_cast<const char*>(sqlite3_column_text(stmt, 5)));
        sstream << ", ";
        put_value(sstream, "Collect Time", "long", sqlite3_column_int64(stmt, 6));

        sstream << "] }";

        count++;
    }

    sstream << " ] } }"; //1:<]> - measurements list, 2:<}> - activities row, 3:<]> activities list, 4:<}> - json close

    if(count==0)
        return false;

    string data = sstream.str();

    /* get a curl handle */
    curl = curl_easy_init();
    if(curl) {

        struct curl_slist *headers = NULL;

        /* First set the URL that is about to receive our POST. This URL can
           just as well be a https:// URL if that is what should receive the
           data. */
        std::string url = config.server_url + "activity";
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_COOKIEJAR, global_cookies->data);
        /* Perform the request, res will get the return code */
        res = curl_easy_perform(curl);
        /* Check for errors */
        if(res != CURLE_OK){
            fprintf(stderr, "curl_easy_perform() failed: %s\n",
                    curl_easy_strerror(res));
        }

        long http_code = 0;
        curl_easy_getinfo (curl, CURLINFO_RESPONSE_CODE, &http_code);
        printf("\nCODE: %li\n", http_code);

        if(http_code==201){

        }

        /* always cleanup */
        curl_easy_cleanup(curl);
//        curl_global_cleanup();
    }
    return true;
}

int send_data(){
//    static int rc = sqlite3_open(SQLITE_PATH, &dbSendDemon);
    int rc;
    filter_data();
    filter_time_data();
    sqlite3_stmt *stmt;

    CURL *curl;
    CURLcode res;

    static int max = 10;
    rc = sqlite3_prepare_v2(dbSendDemon, DbQueries::SelectNonSentMetrics().c_str(), -1, &stmt, 0);
    sqlite3_bind_int(stmt, 1, max);
    std::vector<int> ids = std::vector<int>();

    int count = 0;

    std::stringstream sstream;
    sstream << "{ \"activities\":[ ";

    while (sqlite3_step(stmt) == SQLITE_ROW){
        if(count > 0)
            sstream << ", ";
        sstream << "{ ";


        ids.push_back(sqlite3_column_int(stmt, COL_ID));
        sstream << "\"start_time\":\"" << sqlite3_column_int64(stmt, COL_CONNECT_TIME) <<"\", ";
        sstream << "\"end_time\":\"" << sqlite3_column_int64(stmt, COL_DISCONNECT_TIME) <<"\", ";
        sstream << "\"browser_url\":\"" << sqlite3_column_text(stmt, COL_URL) <<"\", ";
        sstream << "\"browser_title\":\"" << sqlite3_column_text(stmt, COL_WINDOW_TITLE) <<"\", ";
        sstream << "\"ip_address\":\"" << "" <<"\", ";
        sstream << "\"mac_address\":\"" << "" <<"\", ";
        sstream << "\"executable_name\":\"" << sqlite3_column_text(stmt, COL_PATH) <<"\"";
        sstream << "} ";

        count++;
    }

    sstream << " ] } "; //1:<]> - measurements list, 2:<}> - activities row, 3:<]> activities list, 4:<}> - json close

    sqlite3_finalize(stmt);

    if(count==0)
        return 0;

    string data = sstream.str();

    printf("%s\n", data.c_str());

    /* get a curl handle */
    curl = curl_easy_init();
    if(curl) {


        /* First set the URL that is about to receive our POST. This URL can
           just as well be a https:// URL if that is what should receive the
           data. */
        std::string url = config.server_url + "activity";
        curl_easy_setopt(curl, CURLOPT_URL, url.c_str());
        curl_easy_setopt(curl, CURLOPT_COOKIELIST, global_cookies->data);

        const char* tmp = data.c_str();

        struct curl_httppost *formpost=NULL;
        struct curl_httppost *lastptr=NULL;

        curl_formadd(&formpost,
                     &lastptr,
                     CURLFORM_COPYNAME, "activity",
                     CURLFORM_COPYCONTENTS, tmp,
                     CURLFORM_END);
        curl_easy_setopt(curl, CURLOPT_HTTPPOST, formpost);

        /* Perform the request, res will get the return code */
        res = curl_easy_perform(curl);
        /* Check for errors */
        if(res != CURLE_OK){
            fprintf(stderr, "curl_easy_perform() failed: %s\n",
                    curl_easy_strerror(res));
        }

        long http_code = 0;
        curl_easy_getinfo (curl, CURLINFO_RESPONSE_CODE, &http_code);
        printf("\nCODE: %li\n", http_code);


        if(http_code==201){

            std::stringstream sstreamUpdate;
            sstreamUpdate << DbQueries::UpdateToSent().c_str();
            std::vector<int>::iterator it = ids.begin();
            sstreamUpdate << "Id = " << *it;
            for (it++; it != ids.end(); ++it){
                sstreamUpdate << " OR ";
                sstreamUpdate << "Id = " << *it;
            }
            sstreamUpdate << ";";
            data = sstreamUpdate.str();
            rc = sqlite3_prepare_v2(dbSendDemon, data.c_str(), -1, &stmt, 0);
            sqlite3_step(stmt);

            sqlite3_finalize(stmt);
        }

        /* always cleanup */
        curl_easy_cleanup(curl);
//        curl_global_cleanup();
    }
    return count;
}

struct SendDemonInfo{
    std::string url;
    int pthread_state = 0;
    int deletion;
};

void* send_demon_main(void *arg){//void* send_demon_main

    curl_global_init(CURL_GLOBAL_ALL);

    SendDemonInfo* demonInfo = (SendDemonInfo *) arg;

    int deleteOld = 360;

    bool exit = false;

    int rc = sqlite3_open(SQLITE_PATH, &dbSendDemon);

    while(!exit){

        if((deleteOld>360 && (*demonInfo).deletion>0) || (*demonInfo).deletion == 0){
            sqlite3_stmt *stmt;
            if((*demonInfo).deletion==0){
                rc = sqlite3_prepare_v2(dbSendDemon, DbQueries::DeleteFilteredAndSentMeasurements().c_str(), -1, &stmt, 0);

                sqlite3_step(stmt);
                sqlite3_finalize(stmt);
            }
            else{
                rc = sqlite3_prepare_v2(dbSendDemon, DbQueries::DeleteFilteredAndSentMeasurementsByDate().c_str(), -1, &stmt, 0);
                time_t rawtime;
                struct tm * timeinfo;

                time (&rawtime);
                timeinfo = localtime (&rawtime);

                timeinfo->tm_mday -= (*demonInfo).deletion;
                time_t since = mktime(timeinfo);

                sqlite3_bind_int(stmt, 1, since);

                sqlite3_step(stmt);
                sqlite3_finalize(stmt);
            }
            deleteOld = 0;
        }

        printf("normal\n");

        switch( (*demonInfo).pthread_state )
        {
            case 0: // everything normal
                //printf("normal\n");
                break;
            case -1:
                printf("Closing send demon\n");
                pthread_exit(0);

                break;
            case 1: // Command for extend
                printf("Command\n");
                break;
        }


        int count = send_data();
        if(count > 0)
        {
            std::this_thread::sleep_for(std::chrono::milliseconds(1000));
            continue;
        }

        printf("Gone to sleep.\n");
        std::this_thread::sleep_for(std::chrono::milliseconds(2000));
        deleteOld++;
    }
    return 0;
}