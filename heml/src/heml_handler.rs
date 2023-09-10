extern crate xml;

use xml::writer::EmitterConfig;
use xml::writer::XmlEvent;

pub trait HemlHandler {
    fn open_element(&mut self, name: &str);
    fn close_element(&mut self);
    fn add_attribute(&mut self, name: &str, value: &str);
    fn end_attributes(&mut self);
    fn add_text(&mut self, text: &str);
    fn open_para(&mut self);
    fn close_para(&mut self);
    fn open_enum(&mut self);
    fn close_enum(&mut self);
    fn open_indent(&mut self);
    fn close_indent(&mut self);
    fn add_comment(&mut self, comment: &str);
    fn add_cdata(&mut self, cdata: &str);
    fn open_document(&mut self);
    fn close_document(&mut self);
// TODO    state_changed(???);
// TODO    open_table(&self, row_style: u32, row_name: &str, fields_name: ???)
// TODO    add_row(??? fieldsValue);
// TODO    close_table()
}


pub struct DebugHandler {
    indent: u32,
}

impl DebugHandler {
    pub fn new() -> DebugHandler {
        DebugHandler {
            indent: 0,
        }
    }
    fn pindent(&self) {
        for _ in 1..self.indent {
            print!("| ");
        }
    }
}

impl HemlHandler for DebugHandler {
    fn open_element(&mut self, name: &str) {
        self.pindent();
        println!("[open:{}]",name);
        self.indent+=1;
    }
    fn close_element(&mut self) {
        self.indent-=1;
        self.pindent();
        println!("[close]");
    }
    fn add_attribute(&mut self, name: &str, value: &str) {
        self.pindent();
        println!(" %{}={}",name,value);
    }
    fn end_attributes(&mut self) {
        self.pindent();
        println!(" %%");
    }
    fn add_text(&mut self, text: &str) {
        self.pindent();
        println!("  [text: {}]",text);
    }
    fn open_para(&mut self) {
        self.pindent();
        println!("[open:para]");
        self.indent+=1;
    }
    fn close_para(&mut self) {
        self.indent-=1;
        self.pindent();
        println!("[close:para]");
    }
    fn open_indent(&mut self) {
        self.pindent();
        println!("[open:indent]");
        self.indent+=1;
    }
    fn close_indent(&mut self) {
        self.indent-=1;
        self.pindent();
        println!("[close:indent]");
    }
    fn open_enum(&mut self) {
        self.pindent();
        println!("[open:enum]");
        self.indent+=1;
    }
    fn close_enum(&mut self) {
        self.indent-=1;
        self.pindent();
        println!("[close:enum]");
    }
    fn add_comment(&mut self, comment: &str) {
        self.pindent();
        println!(" /* {} */",comment);
    }
    fn add_cdata(&mut self, cdata: &str) {
        self.pindent();
        println!(" /! {} !/",cdata);
    }
    fn open_document(&mut self) {
        println!("[begin_document]");
        self.indent+=1;
    }
    fn close_document(&mut self) {
        self.indent-=1;
        self.pindent();
        println!("[end_document]");
    }
}
