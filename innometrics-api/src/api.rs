use crate::dto::{ActivitiesReport, AuthRequest, TokenResponse};
use crate::error::ApiResult;

/// Root trait for accessing Innometrics API functions.
pub trait InnometricsService {
    /// `POST /login`
    ///
    /// Login is the only request available for unauthorized users.
    fn login(&self, body: AuthRequest) -> ApiResult<TokenResponse>;

    /// Entry point for all authenticated requests to the rest of the API.
    fn authenticated<'a>(&'a self, authenticator: Box<dyn RequestAuthenticator>) -> Box<dyn AuthenticatedInnometricsService + 'a>;
}

/// Innometrics API available for authenticated users only.
/// Instances of this trait can be obtained from `InnometricsService::authenticated()` method.
///
/// This trait exists to reduce boilerplate associated with passing
/// credentials to each method in a copy-paste way.
pub trait AuthenticatedInnometricsService {
    /// `POST /V1/activity`
    fn post_activities_report(&self, activities: &ActivitiesReport) -> ApiResult<()>;

    /// `GET /V1/activity`
    fn get_activities_report(&self, email: &str) -> ApiResult<ActivitiesReport>;
}

/// Implementors of this trait are capable of adding authentication information to the request.
pub trait RequestAuthenticator {
    fn authenticate(&self, req: reqwest::blocking::RequestBuilder) -> reqwest::blocking::RequestBuilder;
}
