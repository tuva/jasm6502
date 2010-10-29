; This is a test for directive org (*=)
* = $2000
lda #$10
sta $02
sta $c000
ldx #$00
rts
*=$c000
lda #$fa
sta $c000
rts

