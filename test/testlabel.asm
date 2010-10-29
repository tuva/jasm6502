; bajs program
; ett fint bajs program

*=$c000
	lda #$10
	ldx #10
loop1: 	sta $fb,x
	dex
	bne loop1
	jsr ra5
	jsr rb6
	rts
ra5:	lda #0
	sta $02
	rts
rb6	tay
rb7	rts
	
