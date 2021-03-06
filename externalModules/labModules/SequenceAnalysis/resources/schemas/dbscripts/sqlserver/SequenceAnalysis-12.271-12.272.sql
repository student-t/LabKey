CREATE INDEX IDX_alignment_summary_analysis_id_rowid_container ON sequenceanalysis.alignment_summary (analysis_id, rowid, container);

CREATE INDEX IDX_alignment_summary_junction_ref_nt_id_status_alignment_id ON sequenceanalysis.alignment_summary_junction (ref_nt_id, status, alignment_id);
CREATE INDEX IDX_alignment_summary_junction_status_alignment_id ON sequenceanalysis.alignment_summary_junction (status, alignment_id);

CREATE INDEX IDX_ref_nt_sequences_rowid_lineage ON sequenceanalysis.ref_nt_sequences (rowid, lineage);