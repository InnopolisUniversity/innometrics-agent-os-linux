use chrono::prelude::*;
use serde::{Serialize, Deserialize};

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct AuthRequest {
    pub email: String,
    pub password: String,
    #[serde(rename = "projectID")]
    pub project_id: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct TokenResponse {
    pub token: String,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ActivitiesReport {
    pub activities: Vec<ActivityReport>,
}

#[derive(Debug, Clone, Serialize, Deserialize)]
pub struct ActivityReport {
    #[serde(rename = "activityID")]
    pub activity_id: i32,
    #[serde(rename = "userID")]
    pub user_id: String,

    #[serde(rename = "activityType")]
    pub activity_type: String,
    pub executable_name: String,
    pub idle_activity: bool,

    pub browser_title: String,
    pub browser_url: String,

    pub ip_address: String,
    pub mac_address: String,

    #[serde(with = "crate::format::date")]
    pub start_time: DateTime<Utc>,
    #[serde(with = "crate::format::date")]
    pub end_time: DateTime<Utc>,
}

impl Default for ActivityReport {
    fn default() -> Self {
        Self {
            activity_id: 0,
            user_id: "".to_string(),

            activity_type: "os".to_string(),
            executable_name: "".to_string(),
            idle_activity: false,

            browser_title: "".to_string(),
            browser_url: "".to_string(),

            ip_address: "".to_string(),
            mac_address: "".to_string(),

            start_time: Utc::now(),
            end_time: Utc::now(),
        }
    }
}
