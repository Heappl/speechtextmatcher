#!/bin/sh

pocketsphinx_continuous \
     -infile test.wav \
     -hmm model_parameters/msu_ru_nsh.cd_cont_1000_8gau_16000 \
     -dict etc/msu_ru_nsh.dic \
     -lm etc/msu_ru_nsh.lm.dmp