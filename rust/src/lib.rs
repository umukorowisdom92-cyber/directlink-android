use std::ffi::{CStr, CString};
use std::os::raw::c_char;
use std::sync::{Arc, Mutex};
use serde_json::json;

// Client state
struct ClientState {
    server_url: String,
    token: Option<String>,
}

lazy_static::lazy_static! {
    static ref CLIENT: Arc<Mutex<Option<ClientState>>> = Arc::new(Mutex::new(None));
}

#[no_mangle]
pub extern "C" fn Java_com_directlink_app_DirectLinkClient_init(
    _env: *mut std::ffi::c_void,
    _class: *mut std::ffi::c_void,
    server_url: *const c_char,
) {
    let url = unsafe { CStr::from_ptr(server_url).to_str().unwrap_or("http://localhost:3030") };
    let mut state = CLIENT.lock().unwrap();
    *state = Some(ClientState {
        server_url: url.to_string(),
        token: None,
    });
}

#[no_mangle]
pub extern "C" fn Java_com_directlink_app_DirectLinkClient_register(
    _env: *mut std::ffi::c_void,
    _class: *mut std::ffi::c_void,
    username: *const c_char,
    phone: *const c_char,
    password: *const c_char,
) -> *mut c_char {
    let result = "{\"status\":\"ok\"}".to_string();
    CString::new(result).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn Java_com_directlink_app_DirectLinkClient_login(
    _env: *mut std::ffi::c_void,
    _class: *mut std::ffi::c_void,
    phone: *const c_char,
    password: *const c_char,
) -> *mut c_char {
    let result = "{\"token\":\"test-token\",\"username\":\"user\",\"phone_number\":\"+1234567890\",\"qr_id\":\"test123\"}".to_string();
    CString::new(result).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn Java_com_directlink_app_DirectLinkClient_checkUser(
    _env: *mut std::ffi::c_void,
    _class: *mut std::ffi::c_void,
    phone: *const c_char,
) -> *mut c_char {
    let result = "{\"on_directlink\":true,\"username\":\"user\",\"phone_number\":\"+1234567890\",\"qr_id\":\"test123\"}".to_string();
    CString::new(result).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn Java_com_directlink_app_DirectLinkClient_getContacts(
    _env: *mut std::ffi::c_void,
    _class: *mut std::ffi::c_void,
) -> *mut c_char {
    let result = "[{\"username\":\"alice\",\"phone_number\":\"+1234567890\",\"online\":true}]".to_string();
    CString::new(result).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn Java_com_directlink_app_DirectLinkClient_createGroup(
    _env: *mut std::ffi::c_void,
    _class: *mut std::ffi::c_void,
    name: *const c_char,
    members_json: *const c_char,
) -> *mut c_char {
    let result = "{\"status\":\"created\",\"group_id\":\"test-group\",\"invite_code\":\"TEST123\"}".to_string();
    CString::new(result).unwrap().into_raw()
}

#[no_mangle]
pub extern "C" fn Java_com_directlink_app_DirectLinkClient_freeString(
    _env: *mut std::ffi::c_void,
    _class: *mut std::ffi::c_void,
    ptr: *mut c_char,
) {
    if !ptr.is_null() {
        unsafe { let _ = CString::from_raw(ptr); }
    }
}
