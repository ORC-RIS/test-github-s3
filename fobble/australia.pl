color(red).
color(blue).
color(green).

nextto(Acolor,Bcolor) :- 
	color(Acolor),
	color(Bcolor),
	Acolor\=Bcolor.

australia(WA,NT,SA,Q,NSW,V,T) :-
	nextto(WA,NT),nextto(WA,SA),
	nextto(NT,SA),nextto(NT,Q),
	nextto(SA,Q),nextto(SA,NSW),
	nextto(SA,V),nextto(NSW,Q),
	nextto(V,NSW).
