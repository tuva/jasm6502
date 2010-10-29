* = $2000
; test low/high byte operators
	constant = $abcd
	lda #<$c000
	sta $fb
backl	lda #>$c000
	sta $fc
	lda #<nextl
	sta $fd
	lda #>nextl
	sta $fe
	lda #<backl
	sta $02
	lda #>backl
	sta $03
	lda #<constant
nextl	lda #>constant
	rts
	constant2 = $1000
