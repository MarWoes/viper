#!/bin/bash

################################################################################
#                           PIPELINE CONFIGURATION                             #
################################################################################
#                                                                              #
# This file contains several configuration parameters for the SV calling pipe- #
# line. To include these parameters, just use source <path-to-config.sh> in    #
# the calling script.                                                          #
#                                                                              #
################################################################################

# Main parameters
RAW_INPUT_DIRS=(
  /raw
)
ALIGNMENT_DIR=~/netherlands-test
RESULTS_DIR=~/results-unit-test

AVAILABLE_TOOLS=(socrates breakmer delly gustaf softsv crest pindel sprites breakdancer seeksv oncocnv)
TOOLS=(socrates breakmer delly gustaf softsv crest pindel sprites breakdancer seeksv)

AVAILABLE_STEPS=(alignment control calling quality simulation analysis igv_vis databases filtering xlsx_vis)
STEPS=(igv_vis)

BREAKPOINT_DISTANCE_TOLERANCE=10
BALANCE_RANGE=40
PILEUP_RANGE=40
KMER_RANGE=7
READ_LENGTH=151

VIS_RANGE=30

# System related
THREADS=2
# Threads used for visualization. This usually needs to be smaller than $THREAD since igv multihreading needs way more resources.
VIS_THREADS=2
# Threads used for pileup computation. Since this means loading a lot of BAM-files, this variable should be kept low
PILEUP_THREADS=2

SIMULATION_DIR=$RESULTS_DIR/simulation
NUM_SIMULATION_SAMPLES=2
NUM_SIMULATION_DELS=20
NUM_SIMULATION_INVS=5
NUM_SIMULATION_DUPS=5
SIMULATION_MIN_SV_SIZE=15
SIMULATION_MAX_SV_SIZE=70
SIMULATION_READ_LENGTH=151
# VAF values are considered to be 'percent'
SIMULATION_MIN_VAF=10
SIMULATION_MAX_VAF=35
SIMULATION_PRIMER_LENGTH=26

CONTROL_DIR=$RESULTS_DIR/control
NUM_CONTROL_SAMPLES=5

RAW_BAM_NAME_SEPERATOR="."
MULTIPLY_SAMPLED_SEPERATOR="_"

# Perform analysis using only raw csv files
WIP_ANALYSIS=false

# Filtering parameters
MINIMUM_AFFECTED=1
MAXIMUM_AFFECTED_PERCENTAGE=1
MINIMUM_SUPPORTING_VALUE=90
MINIMUM_SV_SIZE=10
MINIMUM_AMPLICON_COUNT=0
MINIMUM_INCONSISTENCY_SCORE=0.05

# Reference parameters
BOWTIE_INDICES=/genomes/Homo_sapiens.GRCh37.67/bowtie2-2.2.6/Homo_sapiens.GRCh37.67.dna.chromosome.all
FASTA_REF=/mnt/home2/share/Genomes/Homo_sapiens.GRCh37.67/Homo_sapiens.GRCh37.67.dna.chromosome.all.fasta
BWA_REF=/genomes/Homo_sapiens.GRCh37.67/bwa-0.7.13/Homo_sapiens.GRCh37.67.dna.chromosome.all.fasta
TWO_BIT_REF=/genomes/Homo_sapiens.GRCh37.67/blat/Homo_sapiens.GRCh37.67.dna.chromosome.all.2bit
TARGET_REGIONS=/regions/targetRegionsSimple.bed
AMPLICON_FULL_INFORMATION=/regions/ampliconTargetInfoMerged.bed
YARA_REF=/genomes/Homo_sapiens.GRCh37.67/yara-0.9.7/REF.index

# OncoCNV parameters
ONCOCNV_P_VALUE_THRESHOLD=0.0001

# BreaKmer parameters
BREAKMER_GENE_ANNOTATION=/opt/BreaKmer-0.0.7/reference/hg19_refgene_table.txt
KMER_LENGTH=15
BREAKMER_MIN_DEL_LENGTH=15

# gustaf parameters
GUSTAF_MIN_SUPPORT=50
GUSTAF_MIN_SIZE=10

# Pindel parameters
PINDEL_MIN_SIZE=10
PINDEL_MIN_READS=5

#  Other tools
SPRITES=/opt/sprites-0.3.0/build/sprites
BEDTOOLS=/opt/bedtools-2.26.0/bin/bedtools
SAMTOOLS=/opt/samtools-1.2/samtools
SOCRATES=/opt/socrates-1.13.1/socrates-1.13.1-jar-with-dependencies.jar
BREAKMER=/opt/BreaKmer-0.0.7/breakmer.py
CUTADAPT=/opt/cutadapt-1.9.1/bin/cutadapt
JELLYFISH=/opt/jellyfish-2.2.6/bin/jellyfish
BLAT_DIR=/opt/blat-3.6/ # directory must contain binaries 'blat', 'gfClient', 'gfServer' and 'faToTwoBit'
YARA=/opt/seqan-v2.3.2/yara-build/bin/yara_mapper
SEQTK=/opt/seqtk-1.2/seqtk
STELLAR=/opt/seqan-v2.3.2/stellar-build/bin/stellar
GUSTAF=/opt/seqan-v2.3.2/gustaf-build/bin/gustaf
DELLY_SRC_DIR=/opt/delly-0.7.6/src
CAP3=/opt/CAP3/cap3
CREST_DIR=/opt/CREST-1.0.0
BAMTOOLS_DIR=/opt/bamtools-2.4.1
SOFT_SV=/opt/SoftSV-1.4.2/SoftSV
PINDEL_DIR=/opt/Pindel/
PICARD_JAR=/opt/picard-2.9.0/picard-2.9.0.jar
BREAKDANCER_MAX=/opt/breakdancer-1.4.5/build/bin/breakdancer-max
BREAKDANCER_BAM2CFG=/opt/breakdancer-1.4.5/perl/bam2cfg.pl
TABIX=/opt/htslib-1.2/tabix
DBVAR_LINK=ftp://ftp.ncbi.nlm.nih.gov/pub/dbVar/data/Homo_sapiens/by_study/estd214_1000_Genomes_Consortium_Phase_3/vcf/estd214_1000_Genomes_Consortium_Phase_3.GRCh37.submitted.variant_call.germline.vcf.gz
IGV_JAR=/opt/igv-2.3.91/igv.jar
SEEKSV=/opt/seeksv-1.2.1/seeksv/seeksv/seeksv
BWA=/opt/bwa-0.7.13/bwa
ART_ILLUMINA=/opt/art-MountRainier/art_illumina
ART_PROFILER=/opt/art-MountRainier/ART_profiler_illumina/art_profiler_illumina
ONCOCNV_DIR=/opt/oncocnv-6.6

SCRIPTS=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
