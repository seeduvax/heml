mod parser;
mod heml_handler;
use parser::Parser;
use std::env;




fn main() {
    let args: Vec<String> = env::args().collect();
    let in_file=&args[1];
    let mut parser=Parser::new(in_file);
    parser.run();
}
