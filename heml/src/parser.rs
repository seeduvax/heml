use std::fs::File;
use std::io::prelude::*;
use std::io::BufReader;
use std::io::Error;
use std::iter::Iterator;

pub struct Parser {
    chit: Box<dyn Iterator<Item=Result<u8, Error>>>,
    line: u64,
    col: u32,
    tabSize: u32,
}

impl Parser {
    pub fn new(name: &String) -> Parser {
        Parser {
            chit: Box::new(BufReader::new(File::open(name).expect("Open failed")).bytes()),
            line: 0,
            col: 0,
            tabSize: 8,
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
    fn start_comment(&mut self) {
        println!("start comment")
    }
    fn start_cdata(&mut self) {
        println!("start cdata")
    }

    fn elem_name(&mut self) {
        println!("start element")
        
    }
    fn start_element(&mut self) {
        loop {
            match self.next_char() {
                None => {
                    println!("Unexpected EOF");
                    return;
                },
                Some('?') => self.start_meta(),
                Some('#') => self.start_comment(),
                Some('!') => self.start_cdata(),
                _ => {
                    self.elem_name();
                    return
                },
            }
        }
    }

    /**
     * Indent handling.
     */
    fn indent(&mut self) -> bool {
        let mut level = 0u32;
        loop {
            match self.next_char() {
                None => {
                    println!("EOF met");
                    return false;
                },
// puml: state indent {
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
                    println!("end element");
                    return true;
                }
                _ => {}
// puml: }
            }
        }
    }


    pub fn run(&mut self) {
        
        while self.indent() {
        }
    }
}


