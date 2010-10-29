.data
var	.byt "hello!", 0

.bss
crisp	.word $c000

.text
lda var
sta $c000
rts
