awk '{
    if (prev != "") {
        if ($0 ~ /DB2RLIB/) {
            print "Modified " prev;
        } else {
            print prev;
        }
    }
    prev = $0;
}
END { print prev }' file.txt
