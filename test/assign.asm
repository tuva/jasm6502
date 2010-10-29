* = $c000
var = $d000
zpvar = $02
zpbar = $fb
	lda #zpvar
	sta zpvar
	sta $03, x
	ldx var
	sta future
lloop	inx
	cpx #255
	bne lloop
future
	rts

