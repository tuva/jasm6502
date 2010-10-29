*=$c000
	; setup a new interrupt
	sei
	lda $0314
	sta orgirq
	lda $0315
	sta orgirq + 1
	lda #<irq
	sta $0314 
	lda #>irq
	sta $0315
	cli
	rts
	; change background color of the screen border
irq:	inc $d020
	jmp $ea31
orgirq:	.byt 0, 0

	
