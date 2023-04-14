pub trait HemlHandler {
    fn open_element(&self, name: &str);
    fn close_element(&self);
    fn add_attribute(&self, name: &str, value: &str);
    fn add_attributes(&self);
    fn add_text(&self, text: &str);
    fn open_para(&self);
    fn close_para(&self);
    fn open_enum(&self);
    fn close_enum(&self);
    fn open_indent(&self);
    fn close_indent(&self);
    fn add_comment(&self, comment: &str);
    fn add_cdata(&self, cdata: &str);
    fn open_document(&self);
    fn close_document(&self);
// TODO    state_changed(???);
// TODO    open_table(&self, row_style: u32, row_name: &str, fields_name: ???)
// TODO    add_row(??? fieldsValue);
// TODO    close_table()
}


pub struct DebugHandler {
}

impl HemlHandler for DebugHandler {
    fn open_element(&self, name: &str) {
        println!("open element {}",name);
    }
    fn close_element(&self) {
        println!("close element");
    }
    fn add_attribute(&self, name: &str, value: &str) {
        println!(" attr: {}={}",name,value);
    }
    fn add_attributes(&self) {
        println!(" attr completed");
    }
    fn add_text(&self, text: &str) {
        println!("  text: {}",text);
    }
    fn open_para(&self) {
        println!("   para:");
    }
    fn close_para(&self) {
        println!("   :para");
    }
    fn open_indent(&self) {
        println!("   indent:");
    }
    fn close_indent(&self) {
        println!("   :indent");
    }
    fn open_enum(&self) {
        println!("   enum:");
    }
    fn close_enum(&self) {
        println!("   :enum");
    }
    fn add_comment(&self, comment: &str) {
        println!(" /* {} */",comment);
    }
    fn add_cdata(&self, cdata: &str) {
        println!(" /! {} !/",cdata);
    }
    fn open_document(&self) {
        println!("begin_document");
    }
    fn close_document(&self) {
        println!("end_document");
    }
}
