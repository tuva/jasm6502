*=$c000
	jmp ahead
	#include "include1.asm"
ahead	lda #0
	sta $02
	ldx #incval
	cpx #5
	bne inclab
	rts
