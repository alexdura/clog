(defconst metadl-mode-syntax-table
  (let ((table (make-syntax-table)))
    ;; (modify-syntax-entry ?# "<" table)
    (modify-syntax-entry ?/ "< 1" table)
    (modify-syntax-entry ?/ "< 2" table)
    (modify-syntax-entry ?\n "> " table)

     ;; (modify-syntax-entry ?\n "> b" table)
     ;; (modify-syntax-entry ?< "(" table)
     ;; (modify-syntax-entry ?> ")" table)
     ;; (modify-syntax-entry ?/ ". 124b" table)
    table))

;; (set-syntax-table metadl-mode-syntax-table)

(setq metadl-highlights
      '(
        ("EDB\\|OUTPUT\\|IMPORT\\|ID\\|DECL\\|TYPE\\|GENERIC\\|java\\|metadl\\|CFG_SUCC\\|CFG_ENTRY\\|c_index\\|c_enclosing_function\\|c_decl\\|c_type\\|c_name\\|node_to_id\\|c_src_file\\|c_src_line_start\\|c_src_line_end\\|c_src_col_start\\|c_src_col_end" . font-lock-builtin-face)
	(":-\\|\\.\\|'\\|<:\\|:>\\|NOT\\|@\\|=\\|\\!\\|inline" . font-lock-keyword-face)
	("`\\([[:alpha:]]\\|_\\)[[:alnum:]]*" . font-lock-variable-name-face)
	("$\\([[:alpha:]]\\|_\\)[[:alnum:]]*" . font-lock-variable-name-face)
        ))


(define-derived-mode metadl-mode fundamental-mode "MetaDL"
  "major mode for editing MetaDL datalog files."
  :syntax-table metadl-mode-syntax-table
  (setq font-lock-defaults '(metadl-highlights))
  (setq-local comment-start "\/\/ ")
  (setq-local comment-end "")
)

(provide 'metadl-mode)
