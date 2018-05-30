#!/usr/bin/env bash
go vet 2> go-vet.out || [ $? == 1 ]
