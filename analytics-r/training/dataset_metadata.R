# ============================================================
# dataset_metadata.R — Inspect emotion training dataset
# ============================================================

TRAIN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT      <- dirname(TRAIN_DIR)

#' Count images per class in dataset/train/ and dataset/test/.
#' @param dataset_root Path to dataset root (must contain train/ and test/).
dataset_metadata <- function(dataset_root = file.path(ROOT, "data", "dataset")) {
  splits  <- c("train", "test", "val")
  results <- lapply(splits, function(split) {
    split_dir <- file.path(dataset_root, split)
    if (!dir.exists(split_dir)) return(NULL)
    classes <- list.dirs(split_dir, full.names = FALSE, recursive = FALSE)
    if (length(classes) == 0) return(NULL)
    counts <- vapply(classes, function(cls) {
      length(list.files(file.path(split_dir, cls),
                        pattern = "\\.(jpg|jpeg|png|bmp)$",
                        ignore.case = TRUE))
    }, integer(1))
    data.frame(split = split, class = classes, count = counts,
               stringsAsFactors = FALSE)
  })

  meta <- do.call(rbind, Filter(Negate(is.null), results))
  if (is.null(meta) || nrow(meta) == 0) {
    message("No dataset images found under: ", dataset_root)
    return(invisible(NULL))
  }

  # Print summary
  cat("=== Dataset Metadata ===\n")
  for (sp in unique(meta$split)) {
    sub <- meta[meta$split == sp, ]
    cat(sprintf("\n[%s] total: %d\n", sp, sum(sub$count)))
    for (i in seq_len(nrow(sub))) {
      cat(sprintf("  %-15s %4d\n", sub$class[i], sub$count[i]))
    }
    # Class balance check
    sub_counts <- sub$count[sub$count > 0]
    if (length(sub_counts) > 1) {
      imbalance <- max(sub_counts) / min(sub_counts)
      if (imbalance > 3) {
        cat(sprintf("  WARNING: class imbalance ratio %.1fx\n", imbalance))
      }
    }
  }
  invisible(meta)
}

# Run when called directly
if (!interactive()) dataset_metadata()
