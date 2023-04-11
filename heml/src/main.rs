use std::env;
use std::fs::File;
use std::io::prelude::*;
use std::io::BufReader;




fn main() {
    let args: Vec<String> = env::args().collect();
    let in_file=&args[1];
    let r = BufReader::new(File::open(in_file).expect("open failed"));
    let mut it = r.bytes();
    loop {
        match it.next() {
            None => break,
            x => println!("{}", x.unwrap().unwrap()),
        }
    }
}
