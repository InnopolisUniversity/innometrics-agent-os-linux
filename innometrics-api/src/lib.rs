// #[macro_use]
// extern crate quick_error;
#[macro_use]
extern crate serde_derive;
extern crate serde_json;

pub mod api;
pub mod error;

#[cfg(test)]
mod tests {
    use crate::api::*;

    fn get_new_token<S: InnometricsService>(service: &S) -> ApiResult<String> {
        let request = AuthRequest {
            email: "i.tkachenko@innopolis.ru".to_string(),
            password: "Innopolis$2020".to_string(),
            project_id: "".to_string(),
        };
        service.login(request)
            .map(|res| res.token)
    }

    #[test]
    fn test() {
        let adapter = get_adapter();

        let token = get_new_token(&adapter);
        println!("Token: {:?}", token);

        if let Ok(token) = token {
            println!("Getting report");
            let report = adapter.authenticated(&token).get_activities_report("i.tkachenko@innopolis.ru");
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
            let result = adapter.authenticated(&token).post_activities_report(&report);
            println!("Result: {}", result.is_ok());
        }
    }
}