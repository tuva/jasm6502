
*= $c000

ask_question:
		ldx #<hello_msg
		ldy #>hello_msg
		jsr print
		
		ldx #<buffer
		ldy #>buffer
		jsr getline
		
		lda #<buffer
		ldx #>buffer
		sta $fb
		stx $fc
		lda #<correct_name
		ldx #>correct_name
		sta $fd
		stx $fe
		jsr strcmp
		bcc correct
		
		ldx #<wrong_answer
		ldy #>wrong_answer
		jsr print
		jmp ask_question
correct:
		ldx #<correct_answer
		ldy #>correct_answer
		jsr print
		rts

.include "string.asm"
hello_msg:
	.byt "HEY, WHAT'S YOUR NAME?", 13, 0
wrong_answer:
	.byt "WHAT UGLY NAME, TRY AGAIN.", 13, 0
correct_answer:
	.byt "WHAT BEAUTIFUL NAME, YOU MUST BE A GOD!", 13 , 0
correct_name:
	.byt "DAVID", 0
buffer:

