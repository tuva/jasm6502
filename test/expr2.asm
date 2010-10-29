*=$c000
	ldx #msgend - msg
loop	lda msg, x
	sta $0400, x
	dex
	bpl loop
	rts
msg	.byt "hello there"
msgend

