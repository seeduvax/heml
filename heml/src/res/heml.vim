syn clear
syn case match

syn match tdDelim1 "%"
syn match tdDelim2 "}"
syn match tdDelim3 "{[a-zA-Z0-9_\-][a-zA-Z0-9_\-]*"
syn match tdInline1 "{[a-zA-Z0-9_\-][a-zA-Z0-9_\-]*[ 	][ 	]*[^%]*}"
syn match tdInline2 "%%[^%^}]*}"
syn match tdEntry "{entry"
syn match tdTable "{%[^%]*"
syn region tdComment start="{#"  end="#}"
syn region readerComment start="{comment " end="}"
syn region tdCData start="{!"  end="!}"
syn region tdCode start="{code" end="}"
syn region tdTodo start="{todo " end="}"
syn region tdTBC start="{tbc " end="}"
syn region tdTBD start="{tbd " end="}"
syn region tdMeta start="{?" end="}"
syn match tdAttrib "%[a-zA-Z0-9_\-][a-zA-Z0-9_\-]*=" 


hi def link tdDelim1 Type
hi def link tdDelim2 Type
hi def link tdDelim3 Type
hi tdInline1 term=bold gui=bold
hi tdInline2 term=bold gui=bold
hi def link tdComment Comment
hi def link tdEntry Statement
hi def link tdTable Statement
hi def link tdCode Number
hi tdAttrib ctermfg=darkmagenta guifg=darkmagenta
hi readerComment ctermfg=darkblue guifg=darkblue
hi tdFT ctermfg=red guifg=red
hi tdTodo ctermbg=yellow guibg=yellow
hi tdTBC ctermbg=yellow guibg=yellow
hi tdTBD ctermbg=yellow guibg=yellow
hi tdMeta ctermfg=brown guifg=brown
hi tdCData ctermfg=red guifg=red

set formatoptions=1
set lbr
