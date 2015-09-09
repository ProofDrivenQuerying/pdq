#set t svg font "arial,11" dashed size 2000,500
set t svg dashed font "Times,11"

set key off
#set origin 0,0
set logscale x
set logscale y
set xlabel "Dataset size [# edges]"
set ylabel "Time [ms]"

set style line 1  lt 1 lw 1 pt 1 lc rgb "#3769A0"
set style line 2 lt 1 lw 1 pt 2 lc rgb "#5AAA54"
set style line 3 lt 1 lw 1 pt 3 lc rgb "#F3B03B"
set style line 4 lt 1 lw 1 pt 4 lc rgb "#D82737"
set style line 5 lt 1 lw 1 pt 5 lc rgb "#8D468E"
set style line 6 lt 1 lw 1 pt 6 lc rgb "#8F9291"
set style line 8 lt 2 lw 1 pt 7 lc rgb "#F3B03B"
set style line 9 lt 2 lw 1 pt 8 lc rgb "#D82737"
set style line 10 lt 2 lw 1 pt 9 lc rgb "#8D468E"
set style line 11 lt 2 lw 1 pt 10 lc rgb "#8F9291"
