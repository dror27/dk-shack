# Make Book v2

`make_song_book.py` creates a book-sized Typst source file from Markdown lyrics, and can compile it to PDF. It replaces the legacy Jupyter notebook with a dependency-free command-line program.

## Markdown input

Each chapter must start with a level-one heading. Lines ending in `\` are treated as lyric lines, blank lines separate stanzas, and everything after a `---` line is ignored. This matches the existing song files, including their media-link footers.

## Configure a book

Copy `book.example.json` and update it. `source_root` is resolved relative to the configuration file; `sources` accepts one or more Python glob patterns below that root. The example targets the English Volume 1 songs.

Use `"language": "hebrew"` (or `"arabic"`) for right-to-left books. Typst shapes these scripts using the configured font, which must be installed locally.

## Build

From this directory, write inspectable Typst:

```sh
python3 make_song_book.py book.json --typ build/book.typ
```

Generate the Typst source and compile the PDF:

```sh
python3 make_song_book.py book.json --typ build/book.typ --pdf --output-dir build
```

Install Typst with `brew install typst` if needed. The generated `.typ` and `.pdf` remain in `build/`.

## Story book

`make_story_book.py` uses the same JSON source selection but renders each blank-line-separated Markdown block as a justified prose paragraph. The Hebrew Volume 1 story collection is configured in `story-book.example.json`.

Pictures are optional. To include them after a chapter, put supported image files (`.png`, `.jpg`, `.jpeg`, `.webp`, or `.svg`) in `pics/<story title>/` beside the Markdown files. A single picture is centered on its own page; two or more pictures are arranged as a two-column collage.

```sh
python3 make_story_book.py story-book.example.json --typ build/stories.typ --pdf --output-dir build
```