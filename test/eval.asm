*=$1000
label1:
	.byt "evaltest"
label2:
constant1 = 10*50-5-6/3
constant2 = -(-5*5/2)
	lda constant1
	sta 3*3*3
	lda #constant2
	sta constant2
	lda *
	lda * + 5
	sta * + $1000
	cmp #5
	beq * - 3
	lda #label2 - label1 - 1
	sta label1
	lda #<label1
	sta constant1
	lda #>label2
	sta constant2
	lda #<label1/2
	sta 2+*/2
	rts
	
