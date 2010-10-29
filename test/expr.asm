*=$c000
	ldx #11
loop	lda texten, x
	sta $0400, x
	inx
	bpl loop
	rts
texten .byt "hello", "hello"
texten2
