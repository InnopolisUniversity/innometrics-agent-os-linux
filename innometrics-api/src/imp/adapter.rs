use reqwest::{blocking, IntoUrl, Url};

use crate::api::*;
use crate::dto::*;
use crate::error::ApiResult;
use crate::imp::authenticated_adapter::AuthenticatedAdapter;

const INNOMETRICS_BASE_URL: &'static str = "https://innometric.guru:9091";

/// Provides instance of a `reqwest` client.
pub fn get_client() -> blocking::Client {
    blocking::Client::new()
}

/// Implementor of the `InnometricsService`.
pub struct Adapter {
    base_url: Url,
    pub(crate) client: blocking::Client,
}

impl Adapter {
    /// Provides instance of an adapter with a default client.
    pub fn instance() -> Self {
        Self::new(INNOMETRICS_BASE_URL, get_client())
    }

    pub fn new<U: IntoUrl>(base_url: U, client: blocking::Client) -> Self {
        let base_url = base_url.into_url().unwrap();
        Adapter {
            base_url,
            client,
        }
    }

    /// Join path with base URL.
    pub(crate) fn path(&self, path: &str) -> Url {
        self.base_url.join(path).unwrap()
    }
}

impl InnometricsService for Adapter {
    fn login(&self, body: AuthRequest) -> ApiResult<TokenResponse> {
        let url = self.path("/login");
        let res = self.client
            .post(url)
            .json(&body)
            .send()?;
        match res.status() {
            reqwest::StatusCode::OK => Ok(res.json()?),
            _ => Err("Invalid email or password".into()),
        }
    }

    fn authenticated<'a>(&'a self, authenticator: Box<dyn RequestAuthenticator>) -> Box<dyn AuthenticatedInnometricsService + 'a> {
        Box::new(AuthenticatedAdapter::new(self, authenticator))
    }
}
