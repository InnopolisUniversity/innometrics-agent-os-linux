use chrono::prelude::*;

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
    #[serde(rename = "activityType")]
    pub activity_type: String,
    pub browser_title: String,
    pub browser_url: String,
    #[serde(with = "crate::format::date")]
    pub end_time: DateTime<Utc>,
    pub executable_name: String,
    pub idle_activity: bool,
    pub ip_address: String,
    pub mac_address: String,
    #[serde(with = "crate::format::date")]
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
            user_id: "".to_string(),
        }
    }
}
