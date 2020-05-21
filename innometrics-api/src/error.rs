use std::fmt;

use serde_json::error::Error as JsonError;
use reqwest::blocking;

#[derive(Debug)]
pub enum ApiError {
    /// Both request and response errors, including 401 UNAUTHORIZED.
    Request { inner: reqwest::Error },
    /// Not a response type that was expected.
    UnexpectedResponse { inner: blocking::Response },
    /// Serialization error.
    SerdeJson { inner: JsonError },
}

impl ApiError {}

impl fmt::Display for ApiError {
    fn fmt(&self, f: &mut fmt::Formatter) -> fmt::Result {
        write!(f, "{:?}", self)
    }
}

impl std::error::Error for ApiError {}

impl From<JsonError> for ApiError {
    fn from(it: JsonError) -> Self {
        ApiError::SerdeJson { inner: it }
    }
}

impl From<reqwest::Error> for ApiError {
    fn from(e: reqwest::Error) -> Self {
        ApiError::Request { inner: e }
    }
}

impl From<blocking::Response> for ApiError {
    fn from(it: blocking::Response) -> Self {
        ApiError::UnexpectedResponse { inner: it }
    }
}

pub type ApiResult<T> = Result<T, ApiError>;
