use crate::api::*;
use crate::dto::*;
use crate::error::ApiResult;
use crate::ext::RequestBuilderAuthenticatorExt;
use crate::imp::adapter::Adapter;

/// Implementor of the `AuthenticatedInnometricsService`.
pub struct AuthenticatedAdapter<'a> {
    pub adapter: &'a Adapter,
    pub authenticator: Box<dyn RequestAuthenticator>,
}

impl<'a> AuthenticatedAdapter<'a> {
    pub fn new(adapter: &'a Adapter, authenticator: Box<dyn RequestAuthenticator>) -> Self {
        Self {
            adapter,
            authenticator,
        }
    }
}

impl<'a> AuthenticatedInnometricsService for AuthenticatedAdapter<'a> {
    fn post_activities_report(&self, activities: &ActivitiesReport) -> ApiResult<()> {
        let url = self.adapter.path("/V1/activity");
        let res = self.adapter.client
            .post(url)
            .json(activities)
            .authenticate(&*self.authenticator)
            .send()?;
        match res.status() {
            reqwest::StatusCode::OK => Ok(()),
            _ => Err("Invalid email or password".into()),
        }
    }

    fn get_activities_report(&self, email: &str) -> ApiResult<ActivitiesReport> {
        let url = self.adapter.path("/V1/activity");
        let res = self.adapter.client
            .get(url)
            .query(&[("email", email)])
            .authenticate(&*self.authenticator)
            .send()?;
        match res.status() {
            reqwest::StatusCode::OK => Ok(res.json()?),
            _ => Err("Invalid email or password".into()),
        }
    }
}
