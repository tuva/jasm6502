;	raster bars demo
;	by David Schager 2006

numlines = 64
startline = 64
colptr = $fb
count = $02
bars = $4b
numbars = 10
* = $c000
			ldx #numbars - 1
			clc
			lda #0
@			sta bars, x
			adc #16
			dex
			bpl @b
			lda #<buffer
			sta colptr
			lda #>buffer
			sta colptr + 1
			jsr setup_irq
			rts
raster:
			inc $d019
			ldy #0
line:		lda (colptr), y
			ldx $d012
@			cpx $d012
			beq @b
			sta $d020
			sta $d021
			iny
			bpl line
			lda #15
			ldy #6
			ldx $d012
@			cpx $d012
			beq @b
			sta $d020
			sty $d021
			jsr update_bars
			lda #14
			sta $d020
			jmp $ea31
			pla
			tay
			pla
			tax
			pla
			rti
update_bars:
			; clear all 80 bytes of buffer
			ldx #7
			lda #6
@			sta buffer , x
			sta buffer + 8, x
			sta buffer + 16, x
			sta buffer + 24, x
			sta buffer + 32, x
			sta buffer + 40, x
			sta buffer + 48, x
			sta buffer + 56, x
			sta buffer + 64, x
			dex
			bpl @b

			; paint 5 new bars in buffer
			ldx #numbars-1
nextbar:	ldy bars, x
			txa
			pha
			lda sinus, y
			tay
			ldx #endbar - bar - 1
@			lda bar, x
			sta buffer, y
			iny
			dex
			bpl @b
			pla
			tax
			inc bars, x
			dex
			bpl nextbar
			rts
setup_irq:	
			sei
			lda #$7f
			sta $dc0d
			sta $dd0d
			lda #1
			sta $d01a
			lda #$1b
			sta $d011
			lda #<raster
			sta $0314
			lda #>raster
			sta $0315
			lda #startline
			sta $d012
			lda $dc0d
			lda $dd0d
			inc $d019
			cli
			rts
* = $c100
sinus:
.byt 32, 32, 33, 34, 35, 35, 36, 37
.byt 38, 39, 39, 40, 41, 42, 42, 43
.byt 44, 44, 45, 46, 47, 47, 48, 49
.byt 49, 50, 51, 51, 52, 52, 53, 54
.byt 54, 55, 55, 56, 56, 57, 57, 58
.byt 58, 59, 59, 59, 60, 60, 60, 61
.byt 61, 61, 62, 62, 62, 62, 63, 63
.byt 63, 63, 63, 63, 63, 63, 63, 63
.byt 63, 63, 63, 63, 63, 63, 63, 63
.byt 63, 63, 63, 62, 62, 62, 62, 61
.byt 61, 61, 60, 60, 60, 59, 59, 59
.byt 58, 58, 57, 57, 56, 56, 55, 55
.byt 54, 54, 53, 52, 52, 51, 51, 50
.byt 49, 49, 48, 47, 47, 46, 45, 44
.byt 44, 43, 42, 42, 41, 40, 39, 39
.byt 38, 37, 36, 35, 35, 34, 33, 32
.byt 31, 31, 30, 29, 28, 28, 27, 26
.byt 25, 24, 24, 23, 22, 21, 21, 20
.byt 19, 19, 18, 17, 16, 16, 15, 14
.byt 14, 13, 12, 12, 11, 11, 10, 9
.byt 9, 8, 8, 7, 7, 6, 6, 5
.byt 5, 4, 4, 4, 3, 3, 3, 2
.byt 2, 2, 1, 1, 1, 1, 0, 0
.byt 0, 0, 0, 0, 0, 0, 0, 0
.byt 0, 0, 0, 0, 0, 0, 0, 0
.byt 0, 0, 0, 1, 1, 1, 1, 2
.byt 2, 2, 3, 3, 3, 4, 4, 4
.byt 5, 5, 6, 6, 7, 7, 8, 8
.byt 9, 9, 10, 11, 11, 12, 12, 13
.byt 14, 14, 15, 16, 16, 17, 18, 19
.byt 19, 20, 21, 21, 22, 23, 24, 24
.byt 25, 26, 27, 28, 28, 29, 30, 31

bar:
.byt 0, 11, 12, 15, 1, 15, 12, 11, 0
endbar:
buffer:





