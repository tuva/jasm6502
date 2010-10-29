;
;	string.asm
;	string functions
;

;	print
;	Outputs a zeroterminated ascii string to screen.
;
;	args:
;	x = lowbyte of address of string
;	y = highbyte of address of string
;	destroys:
;	none
;
.proc print
			pha
			stx readc + 1
			sty readc + 2
			tya
			pha
			ldy #0
	readc:	lda *, y
			beq return
			jsr $ffd2
			iny
			jmp readc
	return: pla
			tay
			pla
			rts
.endproc

;	getline
;	Gets one line of input from keyboard and puts zeroterminator.
;
;	args:
;	x = lowbyte of address of buffer to store string
;	y = highbyte of address of buffer to store string
;	destroys a, x, y
;
.proc getline
			lda $fb
			pha
			lda $fc
			pha
			stx $fb
			sty $fc
			ldy #0
	getc:	jsr $ffcf
			cmp #13
			beq storz
			sta ($fb), y
			iny
			jmp getc
	storz:	jsr $ffd2
			lda #0
			sta ($fb), y
			pla
			sta $fc
			pla
			sta $fb
			rts
.endproc

;
;	strcmp
;	Compares two strings.
;	The 16bit pointer to first string has to be at zero-page $fb, and 16bit pointer to second at zero-page $fd
;	destroys: none
;
.proc strcmp
			pha
			tya
			pha
			ldy #0
	str1:	lda ($fb), y
	str2:	cmp ($fd), y
			bne noteq
			iny
			cmp #0
			bne str1
			clc
			bcc return
	noteq:	sec
	return:	pla
			tay
			pla
			rts
.endproc
