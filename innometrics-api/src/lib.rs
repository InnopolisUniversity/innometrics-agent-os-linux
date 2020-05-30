// #[macro_use]
// extern crate quick_error;

pub mod api;
pub mod dto;
pub mod error;
pub mod ext;
mod format;
pub mod jwt;
pub mod imp;
pub mod prelude;

#[cfg(test)]
mod tests {
    use mockito::{mock, Matcher};
    use serde_json::json;

    use crate::prelude::*;

    const MOCK_TOKEN: &str = "abcdef.0xdeadbeef.XYZ";

    fn get_new_token<S: InnometricsService>(service: &S) -> ApiResult<TokenHeaderAuthenticator> {
        let _m = mock("POST", "/login")
            .match_body(Matcher::PartialJson(json!({
                "email": "i.tkachenko@innopolis.ru",
                "password": "Innopolis$2020",
                "projectID": "",
            })))
            .with_status(200)
            .with_body(serde_json::to_string(&TokenResponse { token: MOCK_TOKEN.to_string() }).unwrap())
            .create();

        let request = AuthRequest {
            email: "i.tkachenko@innopolis.ru".to_string(),
            password: "Innopolis$2020".to_string(),
            project_id: "".to_string(),
        };
        service.login(request)
            .map(From::from)
    }

    #[test]
    fn test() {
        let adapter = Adapter::instance();

        let token = get_new_token(&adapter);
        println!("Token: {:?}", token);

        // let _m1 = mock("GET", "/V1/activity")
        //     .match_header("Token", MOCK_TOKEN)
        //     .match_query(Matcher::UrlEncoded("email".into(), "i.tkachenko@innopolis.ru".into()))
        //     .with_status(200)
        //     .with_body(serde_json::to_string(&ActivitiesReport {
        //         activities: vec![
        //             // no activities so far
        //         ]
        //     }).unwrap())
        //     .create();

        let _m1_401 = mock("GET", "/V1/activity")
            .match_header("Token", MOCK_TOKEN)
            .match_query(Matcher::UrlEncoded("email".into(), "i.tkachenko@innopolis.ru".into()))
            .with_status(401)
            .create();

        let _m2 = mock("POST", "/V1/activity")
            .match_header("Token", MOCK_TOKEN)
            .match_body(Matcher::JsonString(serde_json::to_string(&ActivitiesReport {
                activities: vec![
                    // no activities so far
                ]
            }).unwrap()))
            .with_status(200)
            .create();

        if let Ok(mut token) = token {
            // token.token = "abc".to_string();
            let adapter = adapter.authenticated(token.boxed());
            println!("Getting report");
            let report = adapter.get_activities_report("i.tkachenko@innopolis.ru");
            println!("Report: {:?}", report);

            println!("Generating report");
            let mut activity_1 = ActivityReport::default();
            activity_1.browser_title = "Example site".to_string();
            activity_1.browser_url = "http://example.com".to_string();
            activity_1.user_id = "i.tkachenko@innopolis.ru".to_string();

            let report = ActivitiesReport {
                activities: vec![activity_1]
            };
            println!("Report: {:?}", report);

            println!("Sending report");
            let result = adapter.post_activities_report(&report);
            println!("Result: {:?}", result);
        }
    }
}
