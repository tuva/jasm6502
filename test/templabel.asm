*=$c000
@	lda #10
	sta $1000
	cmp #50
	bne @-
	beq @+
	jmp @+
@	rts
	rts
