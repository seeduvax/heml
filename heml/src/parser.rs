use std::fs::File;
use std::io::prelude::*;
use std::io::BufReader;
use std::io::Error;
use std::iter::Iterator;
use crate::heml_handler::HemlHandler;
use crate::heml_handler::DebugHandler;

pub struct Parser {
    chit: Box<dyn Iterator<Item=Result<u8, Error>>>,
    line: u64,
    col: u32,
    tabSize: u32,
    handler: Box<dyn HemlHandler>,
}

impl Parser {
    pub fn new(name: &String) -> Parser {
        Parser {
            chit: Box::new(BufReader::new(File::open(name).expect("Open failed")).bytes()),
            line: 0,
            col: 0,
            tabSize: 8,
            handler: Box::new(DebugHandler::new()),
        }
    }
    fn next_char(&mut self) -> Option<char> {
        self.col+=1;
        match self.chit.next() {
            None => return None,
            x=> {
                match x.unwrap().unwrap() as char {
                    '\n' => {
                        self.col=0;
                        self.line+=1;
                        return Some('\n');
                    },
                    y => {
                        self.col+=1;
                        return Some(y);
                    },
                }
            },
        }
    }

    fn start_meta(&mut self) {
        println!("start meta")
    }

    /**
     * State function: comment
     */ 
    fn handle_comment(&mut self) {
        let mut may_end = false;
        let mut comment = String::from("");
        loop {
            match self.next_char() {
                None => {
                    println!("Unexpected EOF");
                    return;
                },
                Some('#') => may_end=true,
                Some('}') => {
                    if may_end {
                        self.handler.add_comment(&comment);
                        return;
                    }
                    else {
                        comment.push('}');
                    }
                }
                x => {
                    may_end=false;
                    comment.push(x.unwrap())
                }
            }
        }
    }
    /**
     * State function: cdata
     */ 
    fn start_cdata(&mut self) {
        let mut may_end = false;
        let mut cdata = String::from("");
        loop {
            match self.next_char() {
                None => {
                    println!("Unexpected EOF");
                    return;
                },
                Some('!') => may_end=true,
                Some('}') => {
                    if may_end {
                        self.handler.add_cdata(&cdata);
                        return;
                    }
                    else {
                        cdata.push('}');
                    }
                }
                x => {
                    may_end=false;
                    cdata.push(x.unwrap())
                }
            }
        }
    }


    /**
     * State function: in line text
     */
    fn in_line_text(&mut self) {
    }

    /**
     * State function: attribute
     */
    fn attribute(&mut self, c: char) {
    }

    /**
     * State function: text or attribute case 1
     * % encountered, following may be attribute, element end or inline text.
     */ 
    fn text_or_attribute_1(&mut self, meta: bool) {
        loop {
            match self.next_char() {
                None => {
                    println!("Unexpected EOF");
                    return;
                },
// puml: state TorA1 {
// puml: state " " as _TorA1
// puml: [*] -> _TorA1
// puml: _TorA1 --> InlineText :%
                Some('%') => {
                    self.handler.end_attributes();
                    self.in_line_text();
                    return;
                },
                Some('\r') => {},
// puml: _TorA1 --> TorA2 :\\n
                Some('\n') => {
                    self.text_or_attribute_2(meta);
                },
// puml: _TorA1 -> [*] :}
                Some('}') => {
                    if meta {
                        // TODO endMetaAttributes() ?
                    }
                    else {
                        self.handler.end_attributes();
                        self.handler.close_element();
                    }
                    return;
                },
// puml: _TorA1 --> _Attr
                x => {
                    self.attribute(x.unwrap());
                    return;
                }
// puml: }
            }
        }
    }
	/**
	 * Text or attribute case 2.
	 * new line encountered in attribute start, follwoing may be:
	 * - attribute indented in new line, 
	 * - structured text.  
	 */
    fn text_or_attribute_2(&mut self, meta: bool) {
        let mut indent= 0u32;
        loop {
            match self.next_char() {
                None => {
                    println!("Unexpected EOF");
                    return;
                },
// puml: state TorA2 {
// puml: state " " as _TorA2
// puml: [*] -> _TorA2
                Some('\r') | Some('\n') => {},
                Some('\t') => {
                    indent+=self.tabSize;
                },
                Some(' ') => {
                    indent+=1;
                },
// puml: _TorA2 --> TorA1 :%
                Some('%') => {
                    self.text_or_attribute_1(meta);
                    return;
                },
// puml: _TorA2 --> SElem :{
                Some('{') => {
                    self.handler.end_attributes();
                    self.indent(indent);
                    return;
                },
// puml: _TorA2 -> [*] :}
                Some('}') => {
                    if meta {
                        // TODO endMetaAttributes() ?
                    }
                    else {
                        self.handler.end_attributes();
                        self.handler.close_element();
                    }
                    return;
                },
                x => {
                    if meta {
                        // TODO endMetaAttributes() ?
                    }
                    else {
                        self.handler.end_attributes();
                        self.handler.close_element();
                    }
                    self.handler.end_attributes();
                    self.indent(indent);
                },
            }
        }
    }
    /**
     * State function: text or attribute case 3
     * space encountered, element name is known, following may be inline text
     * or attributes.
     */ 
    fn text_or_attribute_3(&mut self, meta: bool) {
        loop {
            match self.next_char() {
                None => {
                    println!("Unexpected EOF");
                    return;
                },
// puml: state TorA3 {
// puml: state " " as _TorA3 
// puml: [*] -> _TorA3
// puml: _TorA3 --> TorA1: %
                Some('%') => {
                    self.text_or_attribute_1(meta);
                    return;
                },
                Some('\t') | Some(' ') => {},
// puml: _TorA3 --> TorA2 : \\n
                Some('\n') => {
                    self.text_or_attribute_2(meta);
                    return;
                },
// puml: _TorA3 -> [*] :}
                Some('}') => {
                    if meta {
                        // TODO endMetaAttributes() ?
                    }
                    else {
                        self.handler.end_attributes();
                        self.handler.close_element();
                    }
                    return;
                },
                x => {
                    if meta {
                        // TODO endMetaAttributes() ?
                    }
                    else {
                        self.handler.end_attributes();
                        self.in_line_text();
                    }
                    return;
                },
            }
        }
    }

    /**
     * State function: element name.
     */ 
    fn elem_name(&mut self,c: char, meta: bool) {
        let mut name = String::from("");
        name.push(c);
        loop {
            match self.next_char() {
                None => {
                    println!("Unexpected EOF");
                    return;
                },
                Some('\r') => {},
// puml: state ElemName {
// puml: state " " as _ElemName
// puml: [*] -> _ElemName
// puml: _ElemName --> TorA1 : %
                Some('%')  => {
                    self.handler.open_element(&name);
                    self.text_or_attribute_1(meta);
                    return;
                },
// puml: _ElemName --> TorA2 : \\n
                Some('\n') => {
                    self.handler.open_element(&name);
                    self.text_or_attribute_2(meta);
                    return;
                },
// puml: _ElemName --> TorA3 : <sp>
                Some(' ') => {
                    self.handler.open_element(&name);
                    self.text_or_attribute_3(meta);
                    return;
                },
// puml: _ElemName -> [*] : } 
                Some('}') => {
                    self.handler.open_element(&name);
                    if meta {
                        // TODO endMetaAttributes() ?
                    }
                    else {
                        self.handler.end_attributes();
                        self.handler.close_element();
                    }
                    return;
                }
// puml: _ElemName --> _ElemName
                x => name.push(x.unwrap()),
// puml: }
            }
        }
        
    }

    
    /**
     * state function: element start. 
     */ 
    fn start_element(&mut self) {
        loop {
            match self.next_char() {
                None => {
                    println!("Unexpected EOF");
                    return;
                },
// puml: state SElem {
// puml: }
// puml: SElem --> SMeta :?
                Some('?') => self.start_meta(),
// puml: SElem --> Comment :#
                Some('#') => {
                    self.handle_comment();
                    return;
                }
// puml: SElem --> CData :!
                Some('!') => self.start_cdata(),
// puml: SElem --> ElemName
                x => {
                    self.elem_name(x.unwrap(),false);
                    return
                },
            }
        }
    }

    /**
     * iState function: handling indentation.
     */
    fn indent(&mut self, indent: u32) -> bool {
        let mut level = 0u32;
        loop {
            match self.next_char() {
                None => {
                    println!("EOF met");
                    return false;
                },
// puml: state Indent {
// puml: state " " as _Indent
// puml: [*] -> _Indent
// puml: _Indent --> _Indent : \\t<sp>\\n
                Some('\t') => level+=self.tabSize,
                Some(' ') => level+=1,
// puml: _Indent --> SElem : {
                Some('{') => self.start_element(),
                Some('\r') | Some('\n') => level=0,
// puml: _Indent --> [*] : }
                Some('}') => {
                    self.handler.close_element();
                    return true;
                }
                _ => {}
// puml: }
            }
        }
    }


    pub fn run(&mut self) {
        
        while self.indent(0) {
        }
    }
}


