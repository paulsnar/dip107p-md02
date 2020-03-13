#!/bin/sh

main() {
  local mainclass="$1"; shift
  local basedir="${1%/}/"; shift
  local outfile="$1"; shift
  local filelist=$(mktemp)
  while true; do
    local file="${1#$basedir}"
    if [ -z "$file" ]; then
      break
    else
      shift
    fi
    echo "$file" >> "$filelist"
  done
  local dir=$(pwd)
  cd "$basedir"
  jar -c -f "$outfile" -e "$mainclass" "@$filelist"
  rm "$filelist"
  cd "$dir"
  mv "$basedir/$outfile" "$dir/$outfile"
}

main $@
