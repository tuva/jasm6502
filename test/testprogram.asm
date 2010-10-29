; small nonsense program
*=$1000
 addr1=$02
 ldx #$10
 ldy #$00
loop: lda (addr1),y ; use indirect indexed y addressing
 sta ($fb,x)
 dex
 iny
 bne loop
 rts

