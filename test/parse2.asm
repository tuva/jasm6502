; small nonsense program
 ldx #$10
 ldy #0
 lda ($02),y ; use indirect indexed y addressing
 sta ($fb,x)
 dex
 iny
 bne $10
 rts

