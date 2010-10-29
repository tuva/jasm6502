*= $c000
start	ldx #$00
loop	lda string, x
	sta $0400, x
	inx
	cpx #10
	bne loop
	rts
string	.byt "hello there", 0
	.byt 80, 81,82,83,84,85,86,87,88,89
	ldx #$00
	ldy #$00
	tay
	tax
	rts
	.word $a000,$b000,$c000,$d000,$e000,$f000,$10,0
	.word start, loop,string
