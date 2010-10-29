
;
;	Mini adventure game engine
;	Quick hack by David Schager 2006.05.27


*= $c000

NORTH = 1
EAST = 1 << 1
SOUTH = 1 << 2
WEST = 1 << 3
MAPSIZE = 4	; 4x4

jmp start
.include "string.asm"
start:
			jsr setup
			jsr welcome
			jsr print_loc
main:		ldx #<buffer
			ldy #>buffer
			jsr getline
			jsr process_cmd
			bcc main
			jsr goodbye
			rts

.proc setup
			; clear screen
			lda #147
			jsr $ffd2
			; set location 4 - west beach
			lda #4
			sta loc
			rts
.endproc

.proc welcome
			ldx #<msg
			ldy #>msg
			jsr print
			rts
	msg:	.byt "WELCOME TO MINI ADVENTURE GAME ENGINE.", 13
			.byt "TYPE N,S,E,W TO WALK AROUND, 'QUIT' TO END GAME.", 13, "13, 0
.endproc

.proc process_cmd
			lda #cmdstr_ptr - cmdptr
			lsr
			tax
			dex
			lda #<buffer
			sta $fb
			lda #>buffer
			sta $fc
			ldy #$fe
	@:		iny
			iny
			lda cmdstr_ptr, y
			sta $fd
			lda cmdstr_ptr + 1, y
			sta $fe
			jsr strcmp
			bcc match
			dex
			bpl @b
			jsr dont_understand
			rts
	match:	lda cmdptr, y
			sta $fb
			lda cmdptr + 1, y
			sta $fc
			jmp ($fb)
.endproc

.proc dont_understand
			ldx #<msg
			ldy #>msg
			jsr print
			rts
	msg:	.byt "I DON'T UNDERSTAND", 13, 0
.endproc

.proc action_north
			lda directions
			and #NORTH
			beq cant_go
			lda loc
			sec
			sbc #MAPSIZE
			sta loc
			jsr print_loc
			rts
.endproc

.proc action_east
			lda directions
			and #EAST
			beq cant_go
			inc loc
			jsr print_loc
			rts
.endproc

.proc action_south
			lda directions
			and #SOUTH
			beq cant_go
			lda loc
			clc
			adc #MAPSIZE
			sta loc
			jsr print_loc
			rts
.endproc

.proc action_west
			lda directions
			and #WEST
			beq cant_go
			dec loc
			jsr print_loc
			rts
.endproc

.proc cant_go
			ldx #<msg
			ldy #>msg
			jsr print
			rts
	msg:	.byt "YOU CANNOT GO THAT WAY.", 13, 0
.endproc

.proc action_quit
			sec
			rts
.endproc

.proc goodbye
			ldx #<msg
			ldy #>msg
			jsr print
			rts
	msg:	.byt "GOODBYE, HAVE A GOOD DAY!", 13, 0
.endproc

.proc print_loc
			lda loc
			asl
			tax
			lda rooms, x
			pha
			lda rooms + 1, x
			asl
			tay
	descr:	ldx loc_vector, y
			lda loc_vector + 1, y
			tay
			jsr print
			ldx #<msg
			ldy #>msg
			jsr print
			pla
			sta directions
			
			bit dir
			beq @f
			ldx #<dir_n
			ldy #>dir_n
			jsr print
			
	@:		bit dir + 1
			beq @f
			ldx #<dir_e
			ldy #>dir_e
			jsr print
			
	@:		bit dir + 2
			beq @f
			ldx #<dir_s
			ldy #>dir_s
			jsr print
			
	@:		bit dir + 3
			beq @f
			ldx #<dir_w
			ldy #>dir_w
			jsr print
	@:		lda #13
			jsr $ffd2
			rts
	
	dir:	.byt NORTH, EAST, SOUTH, WEST
	dir_n:	.byt "N ",0
	dir_e:	.byt "E ",0
	dir_s:	.byt "S ",0
	dir_w:	.byt "W ",0
	msg:	.byt "EXITS: ", 0
.endproc

cmdptr:				.word action_north, action_east, action_south, action_west, action_quit
cmdstr_ptr:			.word cmd_n, cmd_e, cmd_s, cmd_w, cmd_quit
	cmd_n:			.byt "N", 0 
	cmd_e:			.byt "E", 0
	cmd_s:			.byt "S", 0
	cmd_w:			.byt "W", 0
	cmd_quit:		.byt "QUIT", 0
loc_beach:			.byt "ON THE BEACH.", 13, 0
loc_wbeach:			.byt "WEST PART OF THE BEACH.", 13, 0
loc_water:			.byt "IN THE WATER ON THE BEACH.", 13, 0
loc_ocastle:		.byt "OUTSIDE THE HUGE CASTLE OF SAND.", 13, 0
loc_icastle:		.byt "INSIDE THE HUGE CASTLE OF SAND.", 13, 0
loc_path:			.byt "ON THE SUNLIT PATH.", 13, 0
loc_bung:			.byt "IN YOUR NICE BUNGALOO.", 13, 0
loc_vector:			.word loc_beach, loc_wbeach, loc_water, loc_ocastle, loc_icastle, loc_path, loc_bung

; Room data (4x4 matrix)
; byte 1 = (bit 0 = can go north, bit 1 = can go east, bit 2 = can go south, bit 3 = can go west)
; byte 2 = index to descriptions vector table

DBEACH 		= NORTH | WEST | SOUTH | EAST 
DWBEACH 	= EAST | (1 << 8) 
DWATER 		= SOUTH | (2 << 8) 
DOCASTLE 	= WEST | EAST | (3 << 8) 
DICASTLE 	= WEST | (4 << 8) 
DPATH 		= NORTH | SOUTH | (5 << 8) 
DBUNG 		= NORTH | (6 << 8) 
rooms:				.word 0, DWATER, 0, 0
					.word DWBEACH, DBEACH, DOCASTLE, DICASTLE
					.word 0, DPATH, 0, 0
					.word 0, DBUNG, 0, 0
loc:				.byt 0
directions:			.byt 0

buffer:
