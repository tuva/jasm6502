*=$c000
	ldx #$0
pr	lda message, x
	sta $0400, x
	inx
	cmp #0
	bne pr
	rts
message	.byt "hello there from jasm6502 assembler!", 0
