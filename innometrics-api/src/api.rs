use chrono::prelude::*;
use reqwest::{blocking::RequestBuilder, IntoUrl, Url};

mod api_date_format {
    use chrono::{DateTime, Utc};
    use serde::{self, Deserialize, Deserializer, Serializer};

    const FORMAT: &'static str = "%Y-%m-%dT%H:%M:%S%.f%z";
    pub fn serialize<S>(date: &DateTime<Utc>, serializer: S) -> Result<S::Ok, S::Error>
        where
            S: Serializer,
    {
        let s = date.format(FORMAT).to_string();
        serializer.serialize_str(&s)
    }

    pub fn deserialize<'de, D>(deserializer: D) -> Result<DateTime<Utc>, D::Error>
        where
            D: Deserializer<'de>,
    {
        let s = <&str>::deserialize(deserializer)?;
        DateTime::parse_from_str(s, FORMAT)
            .map(Into::into)
            .map_err(serde::de::Error::custom)
    }
}

pub struct Adapter {
    base_url: Url,
    client: reqwest::blocking::Client,
}

impl Adapter {
    pub fn new<U: IntoUrl>(base_url: U, client: reqwest::blocking::Client) -> Self {
        Adapter {
            base_url: base_url.into_url().unwrap(),
            client,
        }
    }

    fn url(&self, path: &str) -> Url {
        self.base_url.join(path).unwrap()
    }
}

pub fn get_client() -> reqwest::blocking::Client {
    reqwest::blocking::Client::new()
}

const INNOMETRICS_BASE_URL: &'static str = "https://innometric.guru:9091";

pub fn get_adapter() -> Adapter {
    Adapter::new(INNOMETRICS_BASE_URL, get_client())
}

#[derive(Debug)]
pub enum ApiError {
    Network,
    Unauthorized,
    Other(Box<dyn std::error::Error>),
}

pub type Error = Box<dyn std::error::Error>;
pub type ApiResult<T> = Result<T, Error>;

#[derive(Debug, Serialize)]
pub struct AuthRequest {
    pub email: String,
    pub password: String,
    #[serde(rename = "projectID")]
    pub project_id: String,
}

#[derive(Debug, Deserialize)]
pub struct TokenResponse {
    pub token: String,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ActivitiesReport {
    pub activities: Vec<ActivityReport>,
}

#[derive(Debug, Serialize, Deserialize)]
pub struct ActivityReport {
    #[serde(rename = "activityID")]
    pub activity_id: i32,
    #[serde(rename = "activityType")]
    pub activity_type: String,
    pub browser_title: String,
    pub browser_url: String,
    #[serde(with = "api_date_format")]
    pub end_time: DateTime<Utc>,
    pub executable_name: String,
    pub idle_activity: bool,
    pub ip_address: String,
    pub mac_address: String,
    #[serde(with = "api_date_format")]
    pub start_time: DateTime<Utc>,
    #[serde(rename = "userID")]
    pub user_id: String,
}

impl Default for ActivityReport {
    fn default() -> Self {
        Self {
            activity_id: 0,
            activity_type: "os".to_string(),
            browser_title: "".to_string(),
            browser_url: "".to_string(),
            end_time: Utc::now(),
            executable_name: "".to_string(),
            idle_activity: false,
            ip_address: "".to_string(),
            mac_address: "".to_string(),
            start_time: Utc::now(),
            user_id: "".to_string()
        }
    }
}

pub trait InnometricsService {
    fn login(&self, body: AuthRequest) -> ApiResult<TokenResponse>;

    fn authenticated<'a>(&'a self, token: &str) -> Box<dyn AuthenticatedInnometricsService + 'a>;
}

pub trait AuthenticatedInnometricsService {
    fn post_activities_report(&self, activities: &ActivitiesReport) -> ApiResult<()>;

    fn get_activities_report(&self, email: &str) -> ApiResult<ActivitiesReport>;
}

impl InnometricsService for Adapter {
    fn login(&self, body: AuthRequest) -> ApiResult<TokenResponse> {
        let url = self.url("/login");
        let res = self.client
            .post(url)
            .json(&body)
            .send()?;
        match res.status() {
            reqwest::StatusCode::OK => Ok(res.json()?),
            _ => Err("Invalid email or password".into()),
        }
    }

    fn authenticated<'a>(&'a self, token: &str) -> Box<dyn AuthenticatedInnometricsService + 'a> {
        Box::new(
            AuthenticatedInnometricsServiceImpl::new(
                self,
                Box::new(
                    TokenHeaderAuthenticator::new(token.to_string()))))
    }
}

pub trait RequestAuthenticator {
    fn authenticate(&self, req: reqwest::blocking::RequestBuilder) -> reqwest::blocking::RequestBuilder;
}

pub struct TokenHeaderAuthenticator {
    pub token: String
}

impl TokenHeaderAuthenticator {
    pub fn new(token: String) -> Self {
        TokenHeaderAuthenticator {
            token
        }
    }
}

impl RequestAuthenticator for TokenHeaderAuthenticator {
    fn authenticate(&self, req: RequestBuilder) -> RequestBuilder {
        req.header("Token", &self.token)
    }
}

pub trait RequestBuilderAuthenticatorExt {
    fn authenticate(self, authenticator: &dyn RequestAuthenticator) -> Self;
}

impl RequestBuilderAuthenticatorExt for RequestBuilder {
    fn authenticate(self, authenticator: &dyn RequestAuthenticator) -> Self {
        authenticator.authenticate(self)
    }
}

pub struct AuthenticatedInnometricsServiceImpl<'a> {
    pub adapter: &'a Adapter,
    pub authenticator: Box<dyn RequestAuthenticator>,
}

impl<'a> AuthenticatedInnometricsServiceImpl<'a> {
    pub fn new(adapter: &'a Adapter, authenticator: Box<dyn RequestAuthenticator>) -> Self {
        Self {
            adapter,
            authenticator,
        }
    }
}

impl<'a> AuthenticatedInnometricsService for AuthenticatedInnometricsServiceImpl<'a> {
    fn post_activities_report(&self, activities: &ActivitiesReport) -> ApiResult<()> {
        let url = self.adapter.url("/V1/activity");
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
        let url = self.adapter.url("/V1/activity");
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
