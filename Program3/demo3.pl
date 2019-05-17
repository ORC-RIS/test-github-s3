is_a(A, B) :-
	looks_like(A, B),
	acts_like(A, B).

acts_like(duck, animal1).
acts_like(duck, animal3).
acts_like(dog, animal2).
acts_like(dog, animal4).

looks_like(duck, animal1).
looks_like(dog, animal2).
looks_like(duck, animal3).
looks_like(duck, animal4).