*=$1000
bcc $1000
bcs $1000
beq $1000
bmi $1060
bne $1060
bpl $1060
bvc $1060
bvs $1060
adc #$10
adc $02
adc $02,x
adc $c000
adc $c000,x
adc $c000,y
adc ($02,x)
adc ($02),y
and #$10
and $02
and $02,x
and $c000
and $c000,x
and $c000,y
and ($02,x)
and ($02),y
asl 
asl $02
asl $02,x
asl $c000
asl $c000,x
bit $02
bit $c000
brk
clc
cld
cli
clv
cmp #$10
cmp $02
cmp $02,x
cmp $c000
cmp $c000,x
cmp $c000,y
cmp ($02,x)
cmp ($02),y
cpx #$10
cpx $02
cpx $c000
cpy #$10
cpy $02
cpy $c000
dec $02
dec $02,x
dec $c000
dec $c000,x
dex
dey
eor #$10
eor $02
eor $02,x
eor $c000
eor $c000,x
eor $c000,y
eor ($02,x)
eor ($02),y
inc $02
inc $02,x
inc $c000
inc $c000,x
inx
iny
jmp $c000
jmp ($c000)
jsr $c000
lda #$10
lda $02
lda $02,x
lda $c000
lda $c000,x
lda $c000,y
lda ($02,x)
lda ($02),y
ldx #$10
ldx $02
ldx $02,y
ldx $c000
ldx $c000,y
ldy #$10
ldy $02
ldy $02,x
ldy $c000
ldy $c000,x
lsr 
lsr $02
lsr $02,x
lsr $c000
lsr $c000,x
nop
ora #$10
ora $02
ora $02,x
ora $c000
ora $c000,x
ora $c000,y
ora ($02,x)
ora ($02),y
pha
php
pla
plp
rol 
rol $02
rol $02,x
rol $c000
rol $c000,x
ror 
ror $02
ror $02,x
ror $c000
ror $c000,x
rti
rts
sbc #$10
sbc $02
sbc $02,x
sbc $c000
sbc $c000,x
sbc $c000,y
sbc ($02,x)
sbc ($02),y
sec
sed
sei
sta $02
sta $02,x
sta $c000
sta $c000,x
sta $c000,y
sta ($02,x)
sta ($02),y
stx $02
stx $02,y
stx $c000
sty $02
sty $02,x
sty $c000
tax
tay
tsx
txa
txs
tya

