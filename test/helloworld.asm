; Print a hello world message
* = $c000
	ldx #0
l1	lda msg, x
	cmp #0
	beq l2
	sta $0400, x
	inx
	jmp l1
l2	rts
msg	.byt "helloworld 123456789:;-+!", 0
 
