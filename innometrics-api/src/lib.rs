// #[macro_use]
// extern crate quick_error;
#[macro_use]
extern crate serde_derive;
extern crate serde_json;

pub mod api;
pub mod dto;
pub mod error;
pub mod ext;
mod format;
pub mod imp;
pub mod prelude;

#[cfg(test)]
mod tests {
    use crate::prelude::*;

    fn get_new_token<S: InnometricsService>(service: &S) -> ApiResult<TokenHeaderAuthenticator> {
        let request = AuthRequest {
            email: "i.tkachenko@innopolis.ru".to_string(),
            password: "Innopolis$2020".to_string(),
            project_id: "".to_string(),
        };
        service.login(request)
            .map(|res| res.into())
    }

    #[test]
    fn test() {
        let adapter = Adapter::instance();

        let token = get_new_token(&adapter);
        println!("Token: {:?}", token);

        if let Ok(token) = token {
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
            println!("Result: {}", result.is_ok());
        }
    }
}
