package org.example.network.rpcprotocol;

/**
 * Enum for RPC request types
 */
public enum RequestType {
    LOGIN, LOGOUT, GET_EVENTS, GET_PARTICIPANTS_BY_EVENT, REGISTER_PARTICIPANT,
    GET_EVENTS_WITH_PARTICIPANT_COUNTS, GET_ALL_PARTICIPANTS, REGISTER_USER
}