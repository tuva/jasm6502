*=$c000
	lda #<msg
	ldx #>msg
	jsr print
	jmp addr
addr:	rts

msg:	.byt "HELLO WORLD!", 0
	
	.proc print
		; use self-modifying code to store address
		sta addr + 1
		stx addr + 2
		ldx #0
		; read another byte
	addr:	lda msg, x
		beq return
		inx
		; call kernel's CHROUT routine to output char
		jsr $ffd2
		jmp addr
	return:	rts
	temp: 	.byte 0
	.endproc
