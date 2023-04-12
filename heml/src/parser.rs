use std::fs::File;
use std::io::prelude::*;
use std::io::BufReader;
use std::io::Error;
use std::iter::Iterator;

pub struct Parser {
    chit: Box<dyn Iterator<Item=Result<u8, Error>>>,
    line: u64,
    col: u32,
}

impl Parser {
    pub fn new(name: &String) -> Parser {
        Parser {
            chit: Box::new(BufReader::new(File::open(name).expect("Open failed")).bytes()),
            line: 0,
            col: 0,
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
    }

    fn start_element(&mut self) {
        match self.next_char() {
            None => {
                println!("Unexpected EOF");
                return;
            },
            Some('?') => self.start_meta(),
            _ => {},
        }
    }

    pub fn run(&mut self) {
        loop {
            match self.next_char() {
                None => break,
                Some('{') => self.start_element(),
                 _ => {
                    println!("EOF expected here");
                    break;
                },
            }
        }
    }
}


